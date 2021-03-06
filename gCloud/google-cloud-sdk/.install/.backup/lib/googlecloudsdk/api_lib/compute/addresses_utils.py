# Copyright 2014 Google Inc. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
"""Common classes and functions for addresses."""
import abc
from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.api_lib.compute import request_helper
from googlecloudsdk.api_lib.compute import utils
from googlecloudsdk.command_lib.compute import flags
import ipaddr


class AddressesMutator(base_classes.BaseAsyncMutator):
  """Base class for modifying addresses."""

  @staticmethod
  def Args(parser):
    """Adds common flags for mutating addresses."""
    scope = parser.add_mutually_exclusive_group()

    flags.AddRegionFlag(
        scope,
        resource_type='address',
        operation_type='operate on')

    scope.add_argument(
        '--global',
        action='store_true',
        help='If provided, it is assumed the addresses are global.')

  @property
  def service(self):
    if self.global_request:
      return self.compute.globalAddresses
    else:
      return self.compute.addresses

  @property
  def resource_type(self):
    return 'addresses'

  @abc.abstractmethod
  def CreateGlobalRequests(self, args):
    """Return a list of one of more globally-scoped request."""

  @abc.abstractmethod
  def CreateRegionalRequests(self, args):
    """Return a list of one of more regionally-scoped request."""

  def CreateRequests(self, args):
    self.global_request = getattr(args, 'global')

    if self.global_request:
      return self.CreateGlobalRequests(args)
    else:
      return self.CreateRegionalRequests(args)


class AddressExpander(object):
  """Mixin class for expanding address names to IP address."""

  def GetAddress(self, address_ref):
    """Returns the address resource corresponding to the given reference."""
    errors = []
    res = list(request_helper.MakeRequests(
        requests=[(self.compute.addresses,
                   'Get',
                   self.messages.ComputeAddressesGetRequest(
                       address=address_ref.Name(),
                       project=address_ref.project,
                       region=address_ref.region))],
        http=self.http,
        batch_url=self.batch_url,
        errors=errors,
        custom_get_requests=None))
    if errors:
      utils.RaiseToolException(
          errors,
          error_message='Could not fetch address resource:')
    return res[0]

  def ExpandAddressFlag(self, args, region):
    """Resolves the --address flag value.

    If the value of --address is a name, the regional address is queried.

    Args:
      args: The command-line flags. The flag accessed is --address.
      region: The region.

    Returns:
      If an --address is given, the resolved IP address; otherwise None.
    """
    if not args.address:
      return None

    # Try interpreting the address as IPv4 or IPv6.
    try:
      ipaddr.IPAddress(args.address)
      return args.address
    except ValueError:
      # ipaddr could not resolve as an IPv4 or IPv6 address.
      pass

    # Lookup the address.
    address_ref = self.CreateRegionalReference(
        args.address, region, resource_type='addresses')
    res = self.GetAddress(address_ref)
    return res.address
